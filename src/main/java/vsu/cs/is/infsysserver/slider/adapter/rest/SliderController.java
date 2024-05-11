package vsu.cs.is.infsysserver.slider.adapter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vsu.cs.is.infsysserver.slider.SliderService;
import vsu.cs.is.infsysserver.slider.adapter.rest.api.SliderApi;
import vsu.cs.is.infsysserver.slider.adapter.rest.dto.request.SliderElementCreateRequest;
import vsu.cs.is.infsysserver.slider.adapter.rest.dto.request.SliderElementUpdateRequest;
import vsu.cs.is.infsysserver.slider.adapter.rest.dto.response.SliderElementResponse;

import java.util.Collection;

import static org.springframework.http.ResponseEntity.ok;

@RequestMapping("/api/sliders")
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class SliderController implements SliderApi {

    private final SliderService sliderService;

    @Override
    @GetMapping()
    public ResponseEntity<Collection<SliderElementResponse>> getSlider() {
        return ok(sliderService.getSlider());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<SliderElementResponse> getSliderElementById(@PathVariable Long id) {
        return ok(sliderService.getSliderElementById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<SliderElementResponse> createSliderElement(
            SliderElementCreateRequest sliderElementCreateRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(sliderService.createSliderElement(sliderElementCreateRequest));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<SliderElementResponse> updateSliderElementById(
            @PathVariable Long id,
            @RequestBody SliderElementUpdateRequest sliderElementUpdateRequest
    ) {
        return ok(sliderService.updateSliderElementById(id, sliderElementUpdateRequest));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSliderElementById(@PathVariable Long id) {
        sliderService.deleteSliderElementById(id);

        return ok().build();
    }
}
